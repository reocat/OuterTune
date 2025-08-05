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

1. **Direct Discord CDN Upload** (if token available): Downloads images locally and uploads them directly to Discord's servers
2. **Fallback to Default Assets**: Uses Discord's default "music" icon when uploads aren't possible
3. **Text-only Presence** (final fallback): Shows presence without images if image processing fails

### 4. Enhanced Thumbnail Support
- **Local Download**: Downloads images locally for processing
- **Discord CDN Upload**: Uploads processed images to Discord's CDN for proper display
- **Fallback Images**: Provides fallback to default music icons when external images can't be loaded
- **Better Error Handling**: Gracefully handles null or invalid thumbnail URLs

## How It Works

### External Images
When an external image URL is provided:
1. The system downloads the image locally
2. Uploads it directly to Discord's CDN using the Discord token
3. Uses the returned asset ID for the presence update

### Why This Approach?
Discord RPC doesn't support external image URLs directly. Even though we were sending the correct URLs (as seen in the HTTP Toolkit data), Discord requires images to be uploaded to their CDN and referenced by asset names.

### Fallback Mechanism
If image processing fails:
1. **Primary**: Uploads image to Discord CDN and uses asset ID
2. **Secondary**: Falls back to Discord's default "music" icon
3. **Tertiary**: Shows text-only presence

### Discord Images
Discord attachment images (starting with "attachments") continue to work as before using the `mp:` prefix.

## Benefits

1. **Privacy**: No image URLs are sent to external services
2. **Reliability**: No dependency on third-party APIs that could go down
3. **Performance**: Local processing reduces latency
4. **Compliance**: Better alignment with privacy-focused applications
5. **Proper Thumbnails**: Actual album/artist images are displayed in Discord
6. **Discord Compatibility**: Uses Discord's official CDN for guaranteed compatibility
7. **Android Compatibility**: Uses only JVM-compatible libraries, avoiding Android dependency issues

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
- Downloads images locally
- Uploads them to Discord's CDN
- Uses the proper asset IDs for display
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
- Images are uploaded to Discord's CDN for proper display
- This approach ensures actual thumbnails are shown instead of generic icons
- Uses only JVM-compatible libraries to avoid Android dependency issues 