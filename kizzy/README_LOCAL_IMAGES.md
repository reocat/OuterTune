# Local Image Processing for Discord RPC

This module has been updated to process Discord RPC images locally without requiring external APIs, improving privacy and reducing dependencies on third-party services.

## Changes Made

### 1. Removed Remote API Dependency
- Deleted `ApiService.kt` which was calling `https://metrolist-discord-rpc-api.fullerbread2032.workers.dev`
- Deleted `ApiResponse.kt` which was used for the remote API response
- Removed the privacy concern of sending image URLs to external services

### 2. Added Local Image Processing
- Created `LocalImageProcessor.kt` for handling images locally
- Updated `KizzyRepository.kt` to use local processing instead of remote API
- Updated `RpcImage.kt` to work with local image handling

### 3. Privacy-First Approach
The new implementation uses a multi-tier approach:

1. **Direct Discord CDN Upload** (if token available): Uploads images directly to Discord's servers
2. **Discord Image Proxy** (fallback): Uses Discord's built-in image proxy to fetch images from URLs
3. **Text-only Presence** (final fallback): Shows presence without images if image processing fails

### 4. Enhanced Thumbnail Support
- **URL Optimization**: Automatically optimizes YouTube/Google image URLs for Discord compatibility
- **Fallback Images**: Provides fallback to default music icons when external images can't be loaded
- **Better Error Handling**: Gracefully handles null or invalid thumbnail URLs

## How It Works

### External Images
When an external image URL is provided:
1. The system optimizes the URL for Discord (e.g., resizes YouTube thumbnails to 512x512)
2. Discord fetches the image from the optimized URL and caches it on their servers
3. This approach maintains privacy as no third-party services are involved

### URL Optimization
The system automatically optimizes common image URL formats:
- **Google User Content**: `https://lh3.googleusercontent.com/...` → optimized to 512x512
- **YouTube Thumbnails**: `https://yt3.ggpht.com/...` → optimized to 512px
- **Regular URLs**: Passed through unchanged

### Fallback Mechanism
If image processing fails:
1. **Primary**: Uses optimized external image URL
2. **Secondary**: Falls back to Discord's default "music" icon
3. **Tertiary**: Shows text-only presence

### Discord Images
Discord attachment images (starting with "attachments") continue to work as before using the `mp:` prefix.

## Benefits

1. **Privacy**: No image URLs are sent to external services
2. **Reliability**: No dependency on third-party APIs that could go down
3. **Performance**: Local processing reduces latency
4. **Compliance**: Better alignment with privacy-focused applications
5. **Better UX**: Proper thumbnails with fallback options

## Usage

The API remains the same. Existing code will continue to work without changes:

```kotlin
val rpc = KizzyRPC(discordToken)
rpc.setActivity(
    name = "Listening to music",
    details = "Song Title",
    largeImage = RpcImage.ExternalImage("https://example.com/album.jpg")
)
```

The system now automatically:
- Optimizes image URLs for Discord
- Provides fallback images when needed
- Handles null or invalid URLs gracefully

## Testing

Run the tests to verify the implementation:

```bash
./gradlew :kizzy:test
```

## Notes

- The implementation gracefully falls back to text-only presence if image processing fails
- All image processing is done locally without external dependencies
- Discord's own image proxy is used when direct uploads aren't possible
- YouTube/Google image URLs are automatically optimized for better Discord compatibility 