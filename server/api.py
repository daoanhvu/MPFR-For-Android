import torch
import numpy as np
import cv2
from enum import Enum
from datetime import datetime
from flask import Flask, request, json, jsonify
from flask_restful import Resource, Api
from PIL import Image

app = Flask(__name__)
api = Api(app)

model = torch.hub.load("ultralytics/yolov5", "yolov5s", device=0)


def do_detect(model, im):
    results = model(im)
    detections = results.pandas().xyxy[0]
    detection_results = []
    for index, row in detections.iterrows():
        x1, y1, x2, y2, confidence, class_id, name = int(row['xmin']), int(row['ymin']), int(
            row['xmax']), int(row['ymax']), row['confidence'], row['class'], row['name']
        if confidence > 0.3:
            detection_results.append({
                "x1": x1,
                "y1": y1,
                "x2": x2,
                "y2": y2,
                "confidence": confidence,
                "className": name})

    return detection_results


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
        return app.response_class(status=statusCode, response=json.dumps(responseBody))
    else:
        return app.response_class(status=statusCode)


@app.route('/', methods=['GET'])
def say_hello():
    return app.response_class(status=200, response="Hi There!")


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=9099)
