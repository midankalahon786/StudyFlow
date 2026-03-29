import subprocess
from datetime import datetime
import os

def generate_markdown_log():
    # Run git log command to get date and message
    # Format: YYYY-MM-DD | Commit Message
    cmd = ["git", "log", "--pretty=format:%ad | %s", "--date=short"]
    
    try:
        result = subprocess.run(cmd, capture_output=True, text=True, check=True)
        log_entries = result.stdout.split('\n')
        
        with open("DEVELOPMENT_LOG.md", "w", encoding="utf-8") as f:
            f.write(f"# StudyFlow Development History\n")
            f.write(f"**Last Updated:** {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n\n")
            f.write("| Date | Change Description |\n")
            f.write("| :--- | :--- |\n")
            
            for entry in log_entries:
                if "|" in entry:
                    date, msg = entry.split(" | ", 1)
                    # Escaping pipe characters in messages to avoid breaking MD table
                    msg = msg.replace("|", "\\|")
                    f.write(f"| {date} | {msg} |\n")

        print("Successfully updated DEVELOPMENT_LOG.md")
    except subprocess.CalledProcessError:
        print("Error: Git log failed. Make sure you are in a Git repository with commits.")
    except Exception as e:
        print(f"An error occurred: {e}")

if __name__ == "__main__":
    generate_markdown_log()