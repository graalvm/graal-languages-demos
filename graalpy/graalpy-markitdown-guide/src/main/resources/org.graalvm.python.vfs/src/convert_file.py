from markitdown import MarkItDown

def convert(file: str) -> str: # ①
    md = MarkItDown(enable_plugins=False)
    result = md.convert(file)
    return result.text_content