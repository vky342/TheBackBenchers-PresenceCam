# Dockerfile — non-root runtime, writable caches for insightface & matplotlib
FROM python:3.10-slim

# reduce interactive prompts during apt installs
ENV DEBIAN_FRONTEND=noninteractive

# Install system packages required by OpenCV / building C extensions if needed
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    g++ \
    git \
    cmake \
    pkg-config \
    libglib2.0-0 \
    libsm6 \
    libxext6 \
    libgl1 \
    python3-dev \
 && rm -rf /var/lib/apt/lists/*

# Create a non-root user and home directory
RUN useradd -m -s /bin/bash appuser

# Set HOME and PATH for the new user
ENV HOME=/home/appuser
ENV PATH=$HOME/.local/bin:$PATH

# Ensure InsightFace and Matplotlib use writable directories inside the user's home
ENV INSIGHTFACE_HOME=$HOME/.insightface
ENV MPLCONFIGDIR=$HOME/.config/matplotlib
# Optional: default DB path inside writable area
ENV DB_PATH=$HOME/data/students_db.npz

# Create required directories and give ownership to appuser
RUN mkdir -p $INSIGHTFACE_HOME $MPLCONFIGDIR $HOME/data \
 && chown -R appuser:appuser $HOME

WORKDIR $HOME/app

# Copy code and requirements into image (do this as root)
COPY requirements.txt ./requirements.txt
COPY . .

# Upgrade pip etc and install heavy binary wheels first (as root)
RUN pip install --upgrade pip setuptools wheel cython \
 && pip install --no-cache-dir numpy onnxruntime

# Install Python requirements (as root)
RUN pip install --no-cache-dir -r requirements.txt

# Give ownership of app directory to appuser (again, to be safe)
RUN chown -R appuser:appuser $HOME

# Switch to non-root user for runtime
USER appuser

# Expose the HF/Spaces default port
EXPOSE 7860

# Final runtime command — use uvicorn (not "fastapi run")
CMD ["uvicorn", "model_api:app", "--host", "0.0.0.0", "--port", "7860"]
