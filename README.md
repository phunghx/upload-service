
## Setup

#### 1. Install ffmpeg

- MacOS: `brew install ffmpeg`
- CentOS: `apt-get install ffmpeg`

#### 2. Upload folders & nginx routing

Default setting: `upload_folder = "/home/ubuntu/data/nginx/public/media"`

There are 2 directories created inside this folder: `upload` & `serve`
+ `upload`: save the temporary files from client ( waiting for process)
+ `serve`: the final path to save processed files & serve for client

nginx will route `/static` to `$upload_folder/serve` folder

