import React from 'react';
import { Spinner } from 'react-bootstrap';
import Editor from '@monaco-editor/react';

const OutputPanel = ({ output, loading }) => {
  if (loading) {
    return (
      <div className="text-center p-5">
        <Spinner animation="border" role="status">
          <span className="visually-hidden">Loading...</span>
        </Spinner>
        <p className="mt-3">Disassembling code...</p>
      </div>
    );
  }

  return (
    <div className="output-container">
      <Editor
        height="70vh"
        defaultLanguage="plaintext"
        value={output}
        theme="vs-dark"
        options={{
          readOnly: true,
          minimap: { enabled: false },
          fontSize: 14,
          scrollBeyondLastLine: false,
          wordWrap: 'on',
          automaticLayout: true,
        }}
      />
    </div>
  );
};

export default OutputPanel;