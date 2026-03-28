REM@echo off
rem Save everything directly to output.txt without echoing to console
REM(
REM    tree /f
REM    for /r %%f in (*) do (
REM        if /i not "%%~xf"==".css" if /i not "%%~xf"==".html" (
REM            echo ==== %%f ====
REM            type "%%f"
REM        )
REM    )
REM) > output.txt

@echo off
(
    tree /f
    for /r %%f in (*.css *.html) do (
        echo ==== %%f ====
        type "%%f"
    )
) > output.txt