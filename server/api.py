import torch
import numpy as np
import cv2
import os
import tempfile
from enum import Enum
from datetime import datetime
from flask import Flask, request, json, jsonify, send_from_directory, abort
from flask_restful import Resource, Api
from PIL import Image

from ultralytics import YOLO

app = Flask(__name__)
api = Api(app)

model = YOLO("yolov8n.pt")

PROCESSED_IMAGES = './processedImages/'
lock_file_name = './process.lock'


def do_detect(model, im):
    result = model(im)

    xyxys = result[0].boxes.xyxy.cpu().numpy()

    confs = result[0].boxes.conf.cpu().numpy()
    clss = result[0].boxes.cls.cpu().numpy()

    detection_result = []

    for xyxy, conf, cls in zip(xyxys, confs, clss):
        x1, y1, x2, y2 = xyxy[0], xyxy[1], xyxy[2], xyxy[3]

        detection_result.append({"x1": float(x1),
                                 "y1": float(y1),
                                 "x2": float(x2),
                                 "y2": float(y2),
                                 "confidence": float(conf),
                                 "className": int(cls)
                                 })

    return detection_result


@app.route('/download_image/<filename>', methods=['GET'])
def download_image(filename):
    try:
        return send_from_directory(PROCESSED_IMAGES, filename, True)
    except FileNotFoundError:
        abort(404)


@app.route('/detect', methods=['POST'])
def upload_image():
    if 'file' not in request.files:
        return jsonify({'error': 'File not found'})

    file = request.files['file']
    statusCode = 400

    if file:
        image = Image.open(file.stream)
        image_data = np.array(image)
        results = do_detect(model, image_data)
        # print(results)
        statusCode = 200

    if statusCode == 200:
        # responsedAt = datetime.now(datetime.UTC)
        responseBody = {
            'serviceCode': 0,
            'detections': results
        }
        print(responseBody)
        return app.response_class(status=statusCode, response=json.dumps(responseBody))
    else:
        return app.response_class(status=statusCode)


@app.route('/trigger_processing', methods=['POST'])
def trigger_batch_processing():
    try:
        payload = request.get_json()
        process_folder = payload['folderName']
        transaction_id = payload['transactionId']
        images = [f for f in os.listdir(process_folder) if os.isfile(
            os.path.join(process_folder, f))]
    except:
        abort(500)


@app.route('/', methods=['GET'])
def say_hello():
    return app.response_class(status=200, response="Hi There!")


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=9099)
