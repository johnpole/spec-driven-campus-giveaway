/**
 * Utility to downscale images client-side before upload.
 * If max(width, height) > maxDimension (default 1600px),
 * renders to a canvas maintaining aspect ratio and exports as JPEG.
 */
export async function downscaleImage(
  file: File,
  maxDimension: number = 1600,
  quality: number = 0.85
): Promise<File> {
  if (!file.type.startsWith('image/')) {
    throw new Error('Selected file is not an image');
  }

  return new Promise<File>((resolve, reject) => {
    const img = new Image();
    const objectUrl = URL.createObjectURL(file);

    img.onload = () => {
      URL.revokeObjectURL(objectUrl);

      const width = img.naturalWidth || img.width;
      const height = img.naturalHeight || img.height;

      // If dimensions are already within maximum constraint, preserve original file
      if (width <= maxDimension && height <= maxDimension) {
        resolve(file);
        return;
      }

      // Compute scaled dimensions preserving aspect ratio
      let targetWidth = width;
      let targetHeight = height;

      if (width > height) {
        if (width > maxDimension) {
          targetHeight = Math.round((height * maxDimension) / width);
          targetWidth = maxDimension;
        }
      } else {
        if (height > maxDimension) {
          targetWidth = Math.round((width * maxDimension) / height);
          targetHeight = maxDimension;
        }
      }

      const canvas = document.createElement('canvas');
      canvas.width = targetWidth;
      canvas.height = targetHeight;

      const ctx = canvas.getContext('2d');
      if (!ctx) {
        reject(new Error('Failed to get canvas 2D rendering context'));
        return;
      }

      ctx.imageSmoothingEnabled = true;
      ctx.imageSmoothingQuality = 'high';
      ctx.drawImage(img, 0, 0, targetWidth, targetHeight);

      const outputType = 'image/jpeg';
      canvas.toBlob(
        (blob) => {
          if (!blob) {
            reject(new Error('Canvas image conversion to blob failed'));
            return;
          }

          // Generate file with original base name and .jpg extension
          const originalName = file.name.replace(/\.[^/.]+$/, '');
          const resizedFile = new File([blob], `${originalName}.jpg`, {
            type: outputType,
            lastModified: Date.now()
          });

          resolve(resizedFile);
        },
        outputType,
        quality
      );
    };

    img.onerror = () => {
      URL.revokeObjectURL(objectUrl);
      reject(new Error('Failed to decode image file'));
    };

    img.src = objectUrl;
  });
}
