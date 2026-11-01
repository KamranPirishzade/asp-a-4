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

## Design

| File | Responsibility |
|---|---|
| `Main.java` | Checks the input arguments, loads the image, opens the window, starts the processing and saves the final image |
| `ImageWindow.java` | Displays the image and makes it smaller on the screen if the original image is too big |
| `Processor.java` | Divides the image into squares and processes them using one or multiple threads |
| `SquareAverager.java` | Calculates the average color of each square |

- **One shared image.** I converted the image to `TYPE_INT_RGB`, so each pixel is stored as an integer in the same array. All threads work directly on this array. The window updates every 30 ms, which allows us to see the changes while the image is being processed.
- **Original size.** If the image is larger than the screen, I only resize it for displaying in the window. The actual processing is still done on the original image, and `result.jpg` is also saved in its original size.
- **Squares.** I divided the image into squares and processed them from left to right, row by row. If the image size is not exactly divisible by the square size, the squares at the right and bottom edges are smaller.
- **Averaging.** For each square, I calculate the average red, green and blue values of its pixels. Then I use this average color for the whole square.
- **Visible progress.** At first, the processing was too fast, and it finished in just a few milliseconds. Because of this, I added a small pause after each square to make the progress visible. I adjusted the pause so that mode `S` takes around 10 seconds. For performance testing, I can disable this pause using `-DnoDelay=true`.

## Concurrency

- **Physical concurrency.** In mode `M`, I use `availableProcessors()` to get the number of available CPU processors. On my Mac, it returns 10, so I create 10 threads. This allows the work to run in parallel on multiple CPU cores.
- **Splitting.** I divided the image into horizontal parts, where each part contains complete rows of squares. Each thread gets one part and processes its squares from top to bottom and left to right.
- **Disjoint tasks.** Each thread works on a different part of the image, and no square is shared between threads. This means two worker threads never change the same pixel. Because of this, there is no race condition between the worker threads, so I don't need locks (competition synchronization) to protect the pixel data.
- **Cooperation synchronization.** I used `join()` to make the main thread wait until all worker threads finish their work. This is important because the final image should only be saved after all squares are processed. Without `join()`, the program could save an image that is not completely processed.
- **Why Java.** I chose Java because it supports parallel execution using threads and already has Swing for displaying images and ImageIO for reading and saving JPG files. I don't need any external libraries. In standard CPython, the GIL prevents multiple threads from executing Python code at the same time, so Python threads give only logical concurrency, not physical concurrency. That is why Java was a suitable choice for this experiment.

## Testing

- **Checkerboard image.** I used `samples/checkerboard.jpg`, which is a black and white checkerboard with 10 px cells. When I use a square size of 20, each square contains two black and two white cells, so the expected result is gray. The only exception is the bottom-right square, which is 10 x 10 and contains one white cell, so it stays white. This test also helps me check if the program correctly handles smaller squares at the edges.

- **Large image.** I also tested `samples/large.jpg`, which has a resolution of 6000 x 4000. Since this image is bigger than my screen, the program makes it smaller when displaying it in the window. However, the actual image is still processed at its original resolution, and `result.jpg` is saved as 6000 x 4000.

- **Same result in both modes.** I ran `large.jpg` with square size 20 in mode `S` and mode `M` and compared the two `result.jpg` files with `cmp`. They were identical, so the threads produce exactly the same image as the single-threaded version.

## Results

I ran the tests on my Mac with an Apple M5 processor (10 cores). I used `samples/large.jpg` with a square size of 5 and disabled the delay using `-DnoDelay=true`. I ran each test 3 times to compare the results.

| | Normal | `-Xint` (JIT disabled) |
|---|---|---|
| S, 1 thread | 40 ms | 685–707 ms |
| M, 10 threads | 67–76 ms | 159–162 ms |

**Normal mode:** Using multiple threads actually made the program slower. In this case, calculating the average color is a very simple operation, which takes around 2 ns per pixel. Because the calculation is so fast, memory access and the extra time needed to create 10 threads can affect the performance more than the calculation itself. All cores share the same memory, so more threads cannot read it faster.

I think the JIT compiler may also affect the results. With one thread, the loop may get optimized earlier. But with 10 threads, they may initially run interpreted code, and the processing could finish before the JIT optimizations become useful.

**With `-Xint`:** When I disabled the JIT compiler, using multiple threads made the program around 4.3 times faster. Without JIT optimization, processing each pixel takes more time, so the CPU has more work to do. In this situation, dividing the image between multiple threads gives a clear performance improvement.

This also shows that the threads can execute in parallel on different CPU cores. However, using more threads does not always mean better performance. It depends on how much work each thread needs to do.

The speedup was around 4.3 times instead of 10 times. One possible reason is that the Apple M5 has both performance and efficiency cores. Since I divided the image into equal parts, threads running on slower cores may take longer to finish, and the program needs to wait for all of them.