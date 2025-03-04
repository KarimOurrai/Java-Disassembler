import axios from 'axios';

export const disassembleCode = async (sourceCode, className, type) => {
  try {
    const endpoint = `/api/disassemble/${type}`;
    const response = await axios.post(endpoint, {
      sourceCode,
      className
    });
    
    if (response.data.success) {
      return response.data.result;
    } else {
      throw new Error(response.data.errorMessage || 'Disassembly failed');
    }
  } catch (error) {
    if (error.response) {
      throw new Error(`Server error: ${error.response.data.errorMessage || error.response.statusText}`);
    } else if (error.request) {
      throw new Error('No response from server. Please check your connection.');
    } else {
      throw error;
    }
  }
};