# Image averaging with threads

Kamran Pirishzada - CSCI 6221 Advanced Software Paradigms, Assignment 4

The program shows an image and replaces every square of the given size with its average color. The squares are processed one by one, so the progress is visible in the window. In mode `S` one thread does all the work, in mode `M` one thread per CPU core works on its own part of the image. The result is saved as `result.jpg`.

## How to run

Requirements: JDK 17 or newer (tested with OpenJDK 27 on macOS). No other libraries are needed.

The same commands work on macOS, Windows and Linux, from the repository folder:

```bash
javac -d out -sourcepath src src/Main.java
java -cp out Main samples/large.jpg 20 M
```

| Argument | Meaning |
|---|---|
| `samples/large.jpg` | the image file |
| `20` | square size in pixels |
| `M` | `S` = single-threaded, `M` = multi-threaded |

`result.jpg` is saved in the folder where the command is run. Closing the window stops the program.