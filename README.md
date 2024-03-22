# MFS - My File System

## Introduction

MFS is a custom file system designed to operate within a single directory, providing a unique approach to managing files and directories. This project aims to create a versatile and user-friendly file system that ignores standard operating system subdirectories, allowing for more flexible file organization and management.

## Features

- **Custom File System Structure:** MFS distinguishes between uppercase and lowercase letters, allowing filenames and directory names within the ASCII range. It supports numbers, dot, underscore, and letters (both uppercase and lowercase), enabling users to create meaningful and diverse file and directory names.

- **Entry Point:** The entry point to the file system is a file named `root.mfs`, which contains the names of directories and files. Directories are prefixed with the letter `D`, while files are prefixed with the letter `F`, ensuring clear identification and organization.

- **Nested Directory Support:** MFS supports nested directories, allowing for complex directory structures. Each subdirectory contains a list of its contents in a file named according to the convention `parent-directory-name.mfs`, facilitating easy navigation and management of files and directories.

- **File Operations:** MFS supports various file operations, including creating, moving, copying, and deleting files. Users can also append content to existing files and view the contents of files, providing comprehensive file management capabilities.

- **Directory Operations:** Users can create, remove, and move directories within the file system. MFS ensures error handling for invalid operations, such as attempting to create duplicate directories or move directories in a way that would create cycles in the directory tree.

## Usage

The MFS project provides a command-line interface for interacting with the file system. Users can perform various operations by specifying command names and parameters. Below are some of the supported commands:

- `ls [dir_name]`: Displays the contents of the specified directory.
- `mkdir [dir_name]`: Creates a new directory with the specified name.
- `rmdir [dir_name]`: Removes the specified directory and its contents.
- `mvdir [src_dir] [target_dir]`: Moves a directory to another location within the file system.
- `touch [file_name]`: Creates a new empty file with the specified name.
- `echo [file_name] [content]`: Appends content to an existing file.
- `cat [file_name]`: Displays the contents of a file.
- `delete [file_name]`: Deletes the specified file.
- `copy [src_file] [target_dir]`: Copies a file to a specified directory.
- `mv [src_file] [target_dir]`: Moves a file to a specified directory.
- `help`: Displays a list of available commands and their usage instructions.
