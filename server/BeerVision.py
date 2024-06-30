import base64
import requests
from PIL import Image
from io import BytesIO
import os
import cv2

from ultralytics import YOLO

import json
import time


class BeerVision():
    def __init__(self):
        # OpenAI API Key
        self.api_key = ""
        self.headers = {"Content-Type": "application/json",
                        "Authorization": f"Bearer {self.api_key}"}
        self.model = YOLO("bestyolov8m.pt")
        # self.content_system = """Bạn là một chuyên gia phân tích dữ liệu của Heineiken. Heneiken sẽ có loại bia như sau: Tiger, Bia Viet, StrongBow, Bivina, Larue.
        #         Bạn sẽ được phân công bài toán là dựa trên hình ảnh chụp các cửa hàng, quán bar, nhà hàng, siêu thị ở Việt Nam,
        #         đánh giá mức độ nhận diện thương hiệu của cửa hàng. Từng bối cảnh khác nhau sẽ có cách đánh giá khác nhau. Bạn sẽ được cung cấp thêm các tham số nhận diện được của mô hình object detection giúp bạn đánh giá hiệu quả hơn.Tuy nhiên bạn chỉ trả lời theo chỉ dẫn, hãy bám sát chĩ dẫn và đưa ra chính xác format json dù chỉ có 1 object. Format json đối với nhiều bức ảnh như sau: ```{...} {...} ...``` trong đó ... là các nội dung bạn sẽ điền
        #         Bước 1, hãy xác định bối cảnh trong bức ảnh, có các bối cảnh sẵn như sau: `nhà hàng`, `tiệm tạp hóa`, ``quán nhậu`, `nhà dân`, `lề đường`, `quán vỉa hè`, `siêu thị````.
        #         Nếu bạn đã cực kì cố mà không thể hiểu được bối cảnh, trả lời là `null`.
        #         Bước 2, từng bối cảnh sẽ có cách phân tích riêng. Đối với 'nhà hàng, quán nhậu, nhà dân, lề đường, quán vỉa hè' hãy phân tích bầu không khí, bao gồm các lựa chọn sau: `vui vẻ, sôi động`, `ảm đạm`,`mệt mỏi`,'hát hò, sôi nổi',
        #         và xác định thêm lượng người sử dụng các loại bia của nhãn hàng heneiken, nếu có bia trong  bao gồm các loại như sau: 'không' nếu không ai sử dụng, `ít` nếu dưới 2 người sử dụng, 'trung bình' nếu từ 3 đến 5 người sử dụng, `nhiều` nếu từ 5 người trở lên.
        #         Phân tích thêm hoạt động của khách hàng, không phải của tiếp thị bao gồm: `trò chuyện`,`ăn nhậu`, `cụng ly`, `nhảy múa`, `trêu đùa, kể chuyện cười`.Sau đó đưa ra format json với key như sau: `bối cảnh`, `không khí`, `số lượng người uống bia`, `hoạt động`.
        #         Đối với `tiệm tạp hóa, siêu thị` hãy đánh giá mức độ nhận diện dựa trên số lượng bia, thùng bia nhận diện được từ mô hình detection, mức độ hấp dẫn của nó, bao gồm các mức như sau: `không có`, `mờ nhạt`, `trung bình`, `tốt`, `xuất sắc`. Đưa ra format json với key: `bối cảnh`.`mức độ nhận diện` """

        self.content_system = """
You are a data analyst for Heineken. Heineken offers the following types of beer: Tiger, Bia Viet, StrongBow, Bivina, Larue.
You will be assigned the task of evaluating brand recognition of stores based on photos taken of stores, bars, restaurants, and supermarkets in Vietnam. Each different context will have a different evaluation method. 
You will be provided with additional parameters detected by the object detection model to help you evaluate more effectively. However, you should only respond according to the instructions and provide the exact JSON format even if there is only one object. The JSON format for multiple images is as follows: {...} {...} ... where ... represents the content you will fill in.
Step 1: Identify the context in the photo. The available contexts are: `restaurant`, `grocery store`, `bar`, `private house`, `street`, `sidewalk stall`, `supermarket`. If you cannot determine the context, respond with `null`.
Step 2: Each context will have its own analysis method. For `restaurant`, `bar`, `private house`, `sidewalk`, `street stall`, analyze the atmosphere, including the following options: `joyful`, `lively`, `gloomy`, `tired`, `singing`, `energetic`, 
and determine the number of people consuming Heineken beer if there is any beer present. The categories are: `none` if no one is consuming, `few`if fewer than 2 people are consuming, `average` if 3 to 5 people are consuming, `many` if more than 5 people are consuming. 
Further analyze the activities of customers, excluding marketing activities, including: `chatting`, `drinking`, `clink glasses`, `dancing`, `joking, telling jokes`. Then provide the JSON format with the following keys: `context`, `atmosphere`, `number of people drinking beer`, `activities`.
For `grocery store`, `supermarket` evaluate the brand recognition based on the number of beers and beer cases identified by the detection model, and its attractiveness level, including the following levels: `none`, `faint`, `average`, `good`, `excellent`. Provide the JSON format with the keys: `context`, `brand recognition level`.
"""

    # Function to encode the image
    def encode_image(self, image_path):
        with Image.open(image_path) as img:
            img = img.resize((600, 600))
            buffered = BytesIO()
            img.save(buffered, format="JPEG")
            return base64.b64encode(buffered.getvalue()).decode('utf-8')

    def count_occurrences(self, input_list):
        # Create an empty dictionary to store the counts
        counts = {}
        names = self.model.names
        # Loop through each item in the list
        for item in input_list:
            # If the item is already in the dictionary, increment its count
            if names[item] in counts:
                counts[names[item]] += 1
            # If the item is not in the dictionary, add it with a count of 1
            else:
                counts[names[item]] = 1

        return counts

    def do_detect(self, im, img_name):
        result = self.model(im)

        result[0].plot(line_width=2, save=True,
                       filename="output_images/" + img_name, conf=0.4)
        xyxys = result[0].boxes.xyxy.cpu().numpy()

        confs = result[0].boxes.conf.cpu().numpy()
        clss = result[0].boxes.cls.cpu().numpy()
        count_objects = self.count_occurrences(clss)

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

        return detection_result, count_objects

    def beervision_predict(self, folder_path):

        images2send_messsages = []
        detections_on_images = []
        for img_name in os.listdir(folder_path):
            img_path = os.path.join(folder_path, img_name)

            base64_image = self.encode_image(img_path)
            images2send_messsages.append({
                "type": "image_url",
                "image_url": {
                    "url": f"data:image/jpeg;base64,{base64_image}"
                }
            })

            img = cv2.imread(img_path)
            if img is None:
                print(f"Could not read image {img_name}")
                continue

            detection_result, count_objects = self.do_detect(img, img_name)

            detections_on_images.append(count_objects)

        init_images_prompts = {
            "type": "text",
            "text": f"Bạn hãy đánh giá giúp tôi những bức ảnh sau. Tôi sẽ cung cấp cho bạn thông tin từ mô hình object detection. Mỗi thông tin của mỗi hình sẽ cách nhau bằng dấu ```,```. Đây là thông tin: {str(detections_on_images)}"
        }

        input_images_prompts = [init_images_prompts]

        for i in images2send_messsages:
            input_images_prompts.append(i)

        messages = [{
                    "role": "system", "content": self.content_system
                    },
                    {
                        "role": "user", "content": input_images_prompts
        }
        ]

        payload = {
            "temperature": 0.2,
            "model": "gpt-4o",
            "messages": messages,
            "max_tokens": 1500
        }

        response = requests.post(
            "https://api.openai.com/v1/chat/completions", headers=self.headers, json=payload).json()

        GPT_response = response["choices"][0]["message"]["content"]

        print(GPT_response)

        return detection_result, count_objects, GPT_response


if __name__ == "__main__":
    bot = BeerVision()
    bot.beervision_predict(folder_path="input_images")
