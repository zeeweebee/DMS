@echo off
rem Save everything directly to output.txt without echoing to console
(
    tree /f
    for /r %%f in (*) do (
        if /i not "%%~xf"==".css" if /i not "%%~xf"==".html" (
            echo ==== %%f ====
            type "%%f"
        )
    )
) > output.txt