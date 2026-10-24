import { downscaleImage } from './image-resizer.util';

describe('image-resizer.util', () => {
  function createTestImageFile(width: number, height: number, name = 'test.png', type = 'image/png'): Promise<File> {
    return new Promise((resolve) => {
      const canvas = document.createElement('canvas');
      canvas.width = width;
      canvas.height = height;
      const ctx = canvas.getContext('2d')!;
      ctx.fillStyle = '#D6001C';
      ctx.fillRect(0, 0, width, height);

      canvas.toBlob((blob) => {
        const file = new File([blob!], name, { type });
        resolve(file);
      }, type);
    });
  }

  it('should preserve files that are already within maxDimension', async () => {
    const file = await createTestImageFile(800, 600, 'small.png');
    const result = await downscaleImage(file, 1600);

    expect(result).toBe(file);
    expect(result.name).toBe('small.png');
  });

  it('should downscale oversized images so max dimension is <= 1600', async () => {
    const file = await createTestImageFile(2400, 1200, 'large-wide.png');
    const result = await downscaleImage(file, 1600);

    expect(result).not.toBe(file);
    expect(result.type).toBe('image/jpeg');
    expect(result.name).toBe('large-wide.jpg');

    // Verify resulting image dimensions
    const img = new Image();
    const objectUrl = URL.createObjectURL(result);
    await new Promise<void>((resolve) => {
      img.onload = () => {
        expect(img.width).toBe(1600);
        expect(img.height).toBe(800);
        URL.revokeObjectURL(objectUrl);
        resolve();
      };
      img.src = objectUrl;
    });
  });

  it('should downscale vertical/portrait images correctly', async () => {
    const file = await createTestImageFile(1000, 2000, 'portrait.png');
    const result = await downscaleImage(file, 1600);

    expect(result.type).toBe('image/jpeg');

    const img = new Image();
    const objectUrl = URL.createObjectURL(result);
    await new Promise<void>((resolve) => {
      img.onload = () => {
        expect(img.height).toBe(1600);
        expect(img.width).toBe(800);
        URL.revokeObjectURL(objectUrl);
        resolve();
      };
      img.src = objectUrl;
    });
  });

  it('should reject non-image files with error', async () => {
    const nonImageFile = new File(['plain text content'], 'doc.txt', { type: 'text/plain' });

    await expectAsync(downscaleImage(nonImageFile)).toBeRejectedWithError('Selected file is not an image');
  });
});
