import React, { useState } from 'react';
import { Container, Row, Col, Form, Button, Nav, Alert } from 'react-bootstrap';
import Editor from '@monaco-editor/react';
import OutputPanel from './components/OutputPanel';
import 'bootstrap/dist/css/bootstrap.min.css';
import './App.css';
import { disassembleCode } from './services/api';

function App() {
  const [sourceCode, setSourceCode] = useState(
    'public class HelloWorld {\n    public static void main(String[] args) {\n        System.out.println("Hello, World!");\n    }\n}'
  );
  const [className, setClassName] = useState('HelloWorld');
  const [output, setOutput] = useState('');
  const [activeTab, setActiveTab] = useState('bytecode');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async () => {
    setLoading(true);
    setError(null);
    
    try {
      const result = await disassembleCode(sourceCode, className, activeTab);
      setOutput(result);
    } catch (err) {
      setError(err.message || 'An error occurred during disassembly');
      setOutput('');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container fluid className="app-container">
      <header className="app-header">
        <h1>Java Disassembler</h1>
        <p>Similar to Godbolt, but for Java!</p>
      </header>
      
      <Row className="main-content">
        <Col md={6} className="editor-panel">
          <Form.Group className="mb-3">
            <Form.Label>Class Name</Form.Label>
            <Form.Control 
              type="text" 
              value={className} 
              onChange={(e) => setClassName(e.target.value)} 
              placeholder="Enter class name" 
            />
          </Form.Group>
          
          <div className="editor-container">
            <Editor
              height="70vh"
              defaultLanguage="java"
              value={sourceCode}
              onChange={setSourceCode}
              theme="vs-dark"
              options={{
                minimap: { enabled: false },
                fontSize: 14,
                scrollBeyondLastLine: false,
                automaticLayout: true,
              }}
            />
          </div>
          
          <Button 
            variant="primary" 
            onClick={handleSubmit} 
            className="mt-3"
            disabled={loading}
          >
            {loading ? 'Processing...' : 'Disassemble'}
          </Button>
        </Col>
        
        <Col md={6} className="output-panel">
          <Nav variant="tabs" className="mb-3">
            <Nav.Item>
              <Nav.Link 
                active={activeTab === 'bytecode'} 
                onClick={() => setActiveTab('bytecode')}
              >
                Bytecode
              </Nav.Link>
            </Nav.Item>
            <Nav.Item>
              <Nav.Link 
                active={activeTab === 'jit'} 
                onClick={() => setActiveTab('jit')}
              >
                JIT Assembly
              </Nav.Link>
            </Nav.Item>
            <Nav.Item>
              <Nav.Link 
                active={activeTab === 'aot'} 
                onClick={() => setActiveTab('aot')}
              >
                AOT Assembly
              </Nav.Link>
            </Nav.Item>
          </Nav>
          
          {error && (
            <Alert variant="danger">
              {error}
            </Alert>
          )}
          
          <OutputPanel output={output} loading={loading} />
        </Col>
      </Row>
      
      <footer className="app-footer">
        <p>&copy; {new Date().getFullYear()} Java Disassembler</p>
      </footer>
    </Container>
  );
}

export default App;