import streamlit as st
import tkinter as tk
from tkinter import filedialog
import cv2
import os
import torch
from detect import do_detect
from ultralytics import YOLO


def file_select():
    root = tk.Tk()
    root.withdraw()  # Hide the main tkinter window
    folder_selected = filedialog.askdirectory()
    root.destroy()  # Close the tkinter window
    return folder_selected


def process_images(folder_path, model):
    image_files = [f for f in os.listdir(
        folder_path) if os.isfile(os.path.join(folder_path, f))]
    st.write(folder_path)
    for f in image_files:
        image = cv2.imread(f)
        img_rgb = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
        detections = do_detect(model=model, im=img_rgb)
        print(detections)
        st.write(detections)


def main():
    model = YOLO("yolov8n.pt")

    st.write("Image folder")
    if st.button('Choose Folder'):
        # folder_path = file_select()
        folder_path = '/mnt/c/projects/beer-vision/test-data'
        if folder_path:
            st.success(f'Selected folder: {folder_path}')
            if st.button("Process"):
                if folder_path == None:
                    st.write(
                        "You have to choose folder that contains images first.")
                else:
                    process_images(folder_path=folder_path, model=model)
        else:
            st.warning('No folder selected')


if __name__ == "__main__":
    main()
