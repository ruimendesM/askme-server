# AskMe Server

Spring Boot Server for AskMe App - A real-time messaging API backend built with Kotlin and Spring Boot.
Based on training from [Philipp Lackner](https://github.com/philipplackner/chirp-api/tree/master).

> **Work in progress.**

## Features

This project is organized into several modules:

- **app**: Entry point and main application logic.
- **user**: Handles user management and authentication.
- **chat**: Manages chat functionality, messages, and participants.
- **notification**: Responsible for sending and managing notifications.
- **common**: Shared types and utilities used across modules.

## Setup

1. Update the `yaml` configuration files with your instance details for:
   - RabbitMQ (MQ)
   - MailGun (Emails)
   - Supabase (Database)
   - Redis (Cache)
2. Add the respective passwords and secrets as environment variables.  
   _Do not include secrets in the repository._

## Usage

- Open the project in IntelliJ IDEA.
- Run the application.
