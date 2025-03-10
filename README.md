# Java Disassembler

A web application similar to Godbolt but for Java, allowing you to view Java bytecode and assembly output.

## Features

- Java source code editor with syntax highlighting
- Bytecode disassembly (using javap)
- Native assembly output (using objdump)
- Modern React frontend

## Prerequisites

To use all features of this application, you need:

1. **Java Development Kit (JDK) 17+**
   - Required for basic compilation and bytecode disassembly

2. **binutils**
   - For viewing assembly output
   - macOS: `brew install binutils`
   - Ubuntu/Debian: `apt-get install binutils`
   - Fedora/RHEL: `dnf install binutils`

3. **Node.js and npm**
   - These will be installed automatically by Maven if not present

## Building and Running

1. Clone the repository
2. Build the application:
   ```
   ./mvnw clean install
   ```
3. Run the application:
   ```
   ./mvnw spring-boot:run
   ```
4. Access the application at http://localhost:8080

## Architecture

### Backend

The Java Disassembler backend is built using Spring Boot and provides a RESTful API for disassembling Java code. The main components are:

#### Controller Layer

- `DisassemblyController`: Handles HTTP requests and delegates to the service layer
  - `/api/disassemble/bytecode` - Endpoint for Java bytecode disassembly
  - `/api/disassemble/jit` - Endpoint for JIT assembly output
  - ~~`/api/disassemble/aot` - Endpoint for AOT assembly output~~

#### Service Layer

- `JavaDisassemblyService`: Contains the core business logic for disassembling Java code
  - Creates temporary files for compilation
  - Compiles Java source code
  - Invokes external tools (javap, JVM with PrintAssembly, GraalVM native-image, objdump)
  - Processes and returns the disassembly output

#### Model Layer

- `CompilationRequest`: Contains the Java source code and class name for disassembly
- `CompilationResponse`: Contains the disassembly result or error message

#### Security Configuration

- `SecurityConfig`: Configures CORS and security settings to allow the frontend to access the API

### Frontend

The frontend is a React application that provides a user interface for interacting with the disassembler. Key components include:

#### Components

- `App`: The main component that manages state and renders the UI
- `OutputPanel`: Displays the disassembly output with syntax highlighting

#### Services

- `api.js`: Contains functions for making API calls to the backend

#### Styling

- CSS files for styling the application

### Integration

The frontend and backend are integrated using the `frontend-maven-plugin`, which:

1. Installs Node.js and npm
2. Installs frontend dependencies
3. Builds the React application
4. Copies the built frontend files to the Spring Boot static resources directory

This allows the frontend and backend to be deployed as a single application.

## Detailed Usage Guide

### Bytecode Disassembly

The bytecode disassembly feature uses the `javap` tool to display the Java bytecode instructions for a given class. This feature:

1. Compiles the Java source code
2. Invokes `javap -c -verbose -p` on the compiled class
3. Returns the bytecode disassembly

This is useful for understanding how Java code is translated into bytecode instructions.

### Native Assembly

The native assembly feature uses the system's `objdump` tool to display the machine code. This feature:

1. Compiles the Java source code
2. Uses `objdump` to disassemble the compiled code
3. Returns the assembly output

This is useful for understanding the low-level machine code generated from your Java code.

### Disabled Features

- AOT Assembly (GraalVM native-image): Temporarily disabled due to long compilation times
- JIT Assembly (PrintAssembly): Currently not supported

## Advanced Configuration

### Application Properties

You can configure the application by modifying the `application.properties` file:

```properties
# Server port
server.port=8080

# Disassembly timeout (in seconds)
app.disassembly.timeout=30

# Temporary directory for compilation
# If not specified, uses the system's default temp directory
app.disassembly.tempdir=/path/to/custom/temp/dir

# Maximum source code size (in characters)
app.disassembly.max-source-size=10000
```

## Troubleshooting

### JIT Assembly Issues

If you're not seeing JIT assembly output:

1. **Check if hsdis is installed**:
   - The HotSpot Disassembler (hsdis) plugin is required for JIT assembly output
   - Make sure you've downloaded the correct version for your platform and placed it in the JRE's lib directory
   - For macOS: `$JAVA_HOME/lib/hsdis-amd64.dylib` or `$JAVA_HOME/lib/hsdis-aarch64.dylib`
   - For Linux: `$JAVA_HOME/lib/hsdis-amd64.so`
   - For Windows: `$JAVA_HOME\lib\hsdis-amd64.dll`

2. **Check JVM version compatibility**:
   - The hsdis plugin must be compatible with your JVM version
   - If you're using a newer JVM, you may need to compile hsdis from source

3. **Enable debugging**:
   - Add `-XX:+TraceCompilerOracle` to the JVM options to see if the compilation filter is working

### Frontend Issues

If you're having issues with the frontend:

1. **Check Node.js and npm versions**:
   - The frontend requires Node.js 14+ and npm 6+
   - Run `node -v` and `npm -v` to verify

2. **Check for build errors**:
   - Look for errors in the Maven build output
   - Try building the frontend manually:
     ```
     cd frontend
     npm install
     npm run build
     ```

3. **Check for CORS issues**:
   - If the frontend can't connect to the backend, check the browser console for CORS errors
   - Make sure the SecurityConfig is properly configured

## Development

### Backend Development

To work on the backend:

1. Import the project into your IDE
2. Run the application from your IDE with the Spring Boot run configuration
3. Make changes to the Java code
4. Use Spring Boot DevTools for automatic restarts

### Frontend Development

To work on the frontend:

1. Start the backend:
   ```
   ./mvnw spring-boot:run
   ```

2. In a separate terminal, start the frontend development server:
   ```
   cd frontend
   npm start
   ```

3. Access the frontend at http://localhost:3000
4. Make changes to the React code
5. The changes will be automatically reloaded

### Adding New Features

To add a new disassembly feature:

1. Add a new endpoint in `DisassemblyController`
2. Implement the corresponding method in `JavaDisassemblyService`
3. Add a new tab in the React frontend
4. Update the API service to call the new endpoint

## Contributing

Contributions are welcome! Here's how you can contribute:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Write tests for your changes
5. Submit a pull request

Please follow the existing code style and include appropriate tests.

## License

[Apache 2.0 License](LICENSE)

## Acknowledgements

- [Compiler Explorer (Godbolt)](https://godbolt.org/) - Inspiration for this project
- [Spring Boot](https://spring.io/projects/spring-boot) - Backend framework
- [React](https://reactjs.org/) - Frontend library
- [Monaco Editor](https://microsoft.github.io/monaco-editor/) - Code editor component
- [GraalVM](https://www.graalvm.org/) - Native compilation
