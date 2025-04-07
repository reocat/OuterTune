#!/bin/bash

# Help function to explain script usage
show_help() {
    echo "Usage: $0 [OPTIONS] [PATH]"
    echo "Cleans hidden byte order marks (BOMs) from source files."
    echo
    echo "Options:"
    echo "  -h, --help     Show this help message"
    echo "  -d, --dry-run  Show what would be changed without making changes"
    echo "  -v, --verbose  Show detailed information about processing"
    echo
    echo "If PATH is not specified, processes current directory recursively"
}

# Parse command line arguments
DRY_RUN=0
VERBOSE=0
TARGET_PATH="."

while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_help
            exit 0
            ;;
        -d|--dry-run)
            DRY_RUN=1
            shift
            ;;
        -v|--verbose)
            VERBOSE=1
            shift
            ;;
        *)
            TARGET_PATH="$1"
            shift
            ;;
    esac
done

# Function to process a single file
process_file() {
    local file="$1"
    # Check if file is binary
    if file "$file" | grep -q "binary"; then
        [[ $VERBOSE -eq 1 ]] && echo "Skipping binary file: $file"
        return
    fi

    # Create a temporary file
    local temp_file=$(mktemp)
    
    # Remove zero-width spaces and other common BOMs
    # 0xEF 0xBB 0xBF (UTF-8 BOM)
    # 0xFE 0xFF (UTF-16 BE BOM)
    # 0xFF 0xFE (UTF-16 LE BOM)
    # 0xE2 0x80 0x8B (Zero-width space)
    sed 's/\xEF\xBB\xBF//g; s/\xFE\xFF//g; s/\xFF\xFE//g; s/\xE2\x80\x8B//g' "$file" > "$temp_file"
    
    # Check if file was modified
    if ! cmp -s "$file" "$temp_file"; then
        if [[ $DRY_RUN -eq 1 ]]; then
            echo "Would clean BOMs from: $file"
        else
            mv "$temp_file" "$file"
            echo "Cleaned BOMs from: $file"
        fi
    else
        [[ $VERBOSE -eq 1 ]] && echo "No BOMs found in: $file"
    fi
    
    # Clean up temporary file if it still exists
    rm -f "$temp_file"
}

# Main script execution
echo "Scanning directory: $TARGET_PATH"
echo "Mode: $([ $DRY_RUN -eq 1 ] && echo "Dry run" || echo "Live run")"

# Find and process all text files recursively
# Focusing on common source code extensions
find "$TARGET_PATH" -type f \( \
    -name "*.kt" -o \
    -name "*.java" -o \
    -name "*.xml" -o \
    -name "*.gradle" -o \
    -name "*.properties" -o \
    -name "*.txt" \
    \) -print0 | while IFS= read -r -d '' file; do
    process_file "$file"
done

echo "Processing complete!"