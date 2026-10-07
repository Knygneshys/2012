#!/bin/bash

# Open the server and two clients in separate macOS Terminal windows.
set -e

if [[ "$(uname -s)" != "Darwin" ]]; then
    echo "This launcher requires macOS and Terminal.app." >&2
    exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

osascript - "$SCRIPT_DIR" <<'APPLESCRIPT'
on run argv
    set projectDir to item 1 of argv
    tell application "Terminal"
        do script ("/bin/bash " & quoted form of (projectDir & "/run-server.sh"))
        repeat 2 times
            do script ("/bin/bash " & quoted form of (projectDir & "/run-client.sh"))
        end repeat
        activate
    end tell
end run
APPLESCRIPT
