# 3D Models Directory

This directory contains 3D models for AR visualization.

## Supported Formats
- GLTF (.gltf, .glb)

## Adding Models
1. Place your 3D model files in this directory
2. Reference them in the code using: `models/your_model.glb`

## Example Usage
```kotlin
modelInstance = modelLoader.createModelInstance(
    assetFileLocation = "models/arrow.glb"
)
```

## Default Models
Currently the app expects:
- `arrow.glb` - Arrow indicator for mission spots

You can download free 3D models from:
- https://sketchfab.com/
- https://poly.pizza/
- https://www.cgtrader.com/free-3d-models