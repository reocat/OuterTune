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

## How It Works

### External Images
When an external image URL is provided:
1. The system first tries to upload the image directly to Discord's CDN using the Discord token
2. If that fails or no token is available, it uses Discord's image proxy
3. Discord fetches the image from the original URL and caches it on their servers
4. This approach maintains privacy as no third-party services are involved

### Discord Images
Discord attachment images (starting with "attachments") continue to work as before using the `mp:` prefix.

## Benefits

1. **Privacy**: No image URLs are sent to external services
2. **Reliability**: No dependency on third-party APIs that could go down
3. **Performance**: Local processing reduces latency
4. **Compliance**: Better alignment with privacy-focused applications

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

## Testing

Run the tests to verify the implementation:

```bash
./gradlew :kizzy:test
```

## Notes

- The implementation gracefully falls back to text-only presence if image processing fails
- All image processing is done locally without external dependencies
- Discord's own image proxy is used when direct uploads aren't possible 