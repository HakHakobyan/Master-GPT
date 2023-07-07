pacman::p_load(rvest, stringr, base64enc, tidyverse, tidytext, textdata, httr)

news <- function(term) {
  
  html_dat <- read_html(paste0("https://news.google.com/search?q=",term,"&hl=en-US&gl=US&ceid=US%3Aen"))
  
  dat <- data.frame(Link = html_dat %>%
                      html_nodes('.VDXfz') %>% 
                      html_attr('href')) %>% 
    mutate(Link = gsub("./articles/","https://news.google.com/articles/",Link))
  
  news_dat <- data.frame(
    Title = html_dat %>%
      html_nodes('.DY5T1d') %>% 
      html_text(),
    
    Time = html_dat %>%
      html_nodes(".SVJrMe")%>% 
      html_text(),
    Link = dat$Link
  )
  
  return(news_dat)
}


AI<-news("AI")

# Google News URLs Decoder

url_list <- as.character(AI$Link)

decode_urls <- function(urls) {
  extracted_urls <- list()
  
  for (url in urls) {
    encoded_part <- sub(".*\\/articles\\/(.*?)\\?.*", "\\1", url)
    decoded_part <- base64decode(encoded_part)
    decoded_string <- rawToChar(decoded_part)
    result <- str_replace(decoded_string, '.*?(http.*)', '\\1')
    new_link <- strsplit(result, "�", fixed = TRUE)[[1]][1]
    
    extracted_urls <- c(extracted_urls, new_link)
  }
  
  return(extracted_urls)
}

url_list1<-unlist(decode_urls(url_list))
clean_urls <- gsub("\\$$", "", url_list1)


# News Article Scraper

results <- data.frame(index = numeric(length(url_list)),
                      output = character(length(url_list)),
                      num_words = numeric(length(url_list)),
                      stringsAsFactors = FALSE)


for (i in seq_along(clean_urls)) {
  url <- clean_urls[i]
  min_words <- 3
  
  # Try to get the web page with GET function
  response <- tryCatch(
    {
      GET(url, config = httr::config(timeout = 1000))
    },
    error = function(e) {
      if (grepl("schannel: failed to receive handshake", e$message) || grepl("Service Unavailable \\(HTTP 503\\)", e$message)) {
        print(paste("Skipping URL due to error:", url))
        return(NULL)
      } else if (grepl("Not Found \\(HTTP 404\\)|Not Modified \\(HTTP 304\\)", e$message)) {
        # Store 0s in the data frame
        results[i, c("index", "output", "num_words")] <- c(i, "", 0)
        return(NULL)
      } else {
        stop(e)
      }
    }
  )
  
  # If response is NULL, skip to next iteration
  if (is.null(response)) next
  
  # Check the status code of the response
  status <- status_code(response)
  
  # If the status code is not 403, proceed with scraping
  if (!(status %in% c(403, 404, 304, 503))) {
    
    html <- read_html(response)
    
    # Extract "p" nodes from HTML using CSS selectors
    p_nodes <- html_nodes(html, "p")
    
    # Filter "p" nodes to keep only those with at least min_words words
    p_nodes <- p_nodes[sapply(p_nodes, function(x) str_count(html_text(x), "\\S+") >= min_words)]
    
    # Extract text from filtered "p" nodes
    text <- html_text(p_nodes)
    
    # Join text into a single string
    full_text <- paste(text, collapse = "\n\n")
    
    # Count the number of words in the article
    num_words <- length(strsplit(full_text, " ")[[1]])
    
    # Store the results in the data frame
    results[i, "index"] <- i
    results[i, "output"] <- full_text
    results[i, "num_words"] <- num_words
    
  } else {
    print(paste("Access denied for URL:", url))
    next
  }
}

results <- results %>% mutate(url = clean_urls)

results$output <- gsub("\\s+", " ", results$output)
results$output <- gsub("\n", "", results$output)

results <- results %>% filter(num_words != 0)

view(results)

# Optional
# subset rows where output column contain any of the specified strings
# results_subset <- subset(results, !grepl("a\\.i\\.|artificial intelligence", results$output, ignore.case = TRUE) & !grepl("AI", results$output))
# clean_results <- anti_join(results, results_subset)