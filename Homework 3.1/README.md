# Homework 3.1

## Description

This folder hosts an implementation of the Branch-and-Bound algorithm for Integer Linear Programming (ILP) problems. After each sub-problem is built, a Two-Phase Simplex solver is run, in order to find the solutions for the sub-problem. The main class contains a sample problem that is used for solving.

The sample problem to solve is:

$$
\begin{alignat}{2}
&\text{maximize: }   && 2x_1 * 3x_2 \\
&\text{subject to: } && 3x_1 * 2x_2 \leq 13 \\
&                    && 4x_1 * 5x_2 \leq 11 \\
&                    && x_1, x_2 \in \mathbb{N}
\end{alignat}
$$

With solution $x_1 = 0$, $x_2 = 2$.

## Building 

Run:

```bash
$ mvn compile
```

## Packaging


Run:

```bash
$ mvn package
```
