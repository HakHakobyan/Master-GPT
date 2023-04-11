import tkinter as tk
from tkinter import filedialog
from docx import Document
import PyPDF2
import openai
from nltk.tokenize import word_tokenize
import nltk
nltk.download('stopwords')
nltk.download('punkt')
from nltk.corpus import stopwords
import datetime

def read_pdf(file_path):
    pdf_text = ""
    with open(file_path, 'rb') as f:
        pdf_reader = PyPDF2.PdfReader(f)
        for page_num in range(len(pdf_reader.pages)):
            page = pdf_reader.pages[page_num]
            pdf_text += page.extract_text()
    return pdf_text

root = tk.Tk()
root.withdraw()

document_path = filedialog.askopenfilename(initialdir = '/', title = 'Select Document', filetypes = (("Word or PDF documents", "*.docx;*.pdf"), ("all files", "*.*")))

text = ""

try:
    if document_path.endswith('.docx'):
        doc = Document(document_path)
        for para in doc.paragraphs:
            text += para.text
    elif document_path.endswith('.pdf'):
        text = read_pdf(document_path)
    else:
        print("Unsupported file format. Please select a Word or PDF document.")
        exit()
except:
    print("Could not open the document. Please check the path and try again.")
    exit()

openai.api_key = "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"
stop_words = set(stopwords.words("english"))
current_date = datetime.datetime.now()
current_year = current_date.year

# def summarize_text(text):
#     MAX_TOKENS = 4097
#     text_chunks = [text[i:i + MAX_TOKENS] for i in range(0, len(text), MAX_TOKENS)]
#     summaries = []
#     for chunk in text_chunks:
#         completions = openai.Completion.create(
#             engine="text-davinci-003",
#             prompt=f"Please generate an abstractive summary of the following text having maximum number of tokens 150:\n{chunk}",
#             max_tokens=150,
#             temperature=0.2
#         )
#         summary = completions.choices[0].text
#         summaries.append(summary)
#     full_summary = " ".join(summaries)
#     # abstractive_summary = openai.Completion.create(
#     #     engine="text-davinci-002",
#     #     prompt=f"Please generate an abstractive summary of the following text:\n{full_summary}",
#     #     max_tokens=100,
#     #     temperature=0.5
#     # ).choices[0].text
#     return full_summary


def tokenize_text(text, token_size=2500):
    tokens = word_tokenize(text)
    token_list = []
    token = ""
    for i, tok in enumerate(tokens):
        token += tok + " "
        if (i+1) % token_size == 0:
            token_list.append(token)
            token = ""
    if token:
        token_list.append(token)
    return token_list

def find_answer(prompt, question_words):
    completions = openai.Completion.create(
        engine="text-davinci-002",
        prompt=prompt,
        max_tokens=300,
        temperature=0.5,
    )
    answer = completions.choices[0].text
    return answer

# print(summarize_text(text))

while True:
    question = input("Please enter your question: ")
    if question.lower() == "exit" or question.lower() == "quit":
        break
    else:
        question_words = set(
            [word.lower() for word in word_tokenize(question) if word.isalpha() and word.lower() not in stop_words])
        text_words = set(
            [word.lower() for word in word_tokenize(text) if word.isalpha() and word.lower() not in stop_words])
        match = question_words.intersection(text_words)
        if match:
            match = list(match)
            tokenized_text = tokenize_text(text)
            for token in tokenized_text:
                prompt = f"Current Date and Year : {current_date} {current_year}\n{token}\nQ: {question}"
                answer = find_answer(prompt, question_words)
                if answer.strip() != "":
                    print(answer)
                    break
            else:
                print("Sorry, I couldn't find an answer to your question from the given data.")
        else:
            print("Sorry, I couldn't find an answer to your question from the given data.")