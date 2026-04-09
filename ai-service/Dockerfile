FROM python:3.10-slim

WORKDIR /app

# 复制依赖文件并安装（使用 CPU 版 TensorFlow 节省空间）
COPY requirements.txt .
RUN python -m pip install --no-cache-dir -i https://mirrors.aliyun.com/pypi/simple/ --trusted-host mirrors.aliyun.com \
    -r requirements.txt

# 复制应用代码
COPY *.py ./
COPY .env ./

# 复制模型文件
COPY models/ ./models/

EXPOSE 8000

HEALTHCHECK --interval=15s --timeout=5s --retries=5 --start-period=60s \
    CMD python -c "import urllib.request; urllib.request.urlopen('http://localhost:8000/health')" || exit 1

CMD ["python", "-m", "uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
