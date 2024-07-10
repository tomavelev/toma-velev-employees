<template>

    <div>
      <input type="file" @change="onFileChange" />
      <button @click="uploadFile">Upload</button>
      <p>&nbsp;</p>
      <table v-if="data.length">
        <thead>
            <tr>
                <th>employee1</th>
                <th>employee2</th>
                <th>projectId</th>
                <th>timeWorkedTogether</th>

            </tr>
        </thead>
        <tbody>
          <tr v-for="(row, rowIndex) in data" :key="rowIndex">
            <td>{{ row.employee1 }}</td>
            <td>{{ row.employee2 }}</td>
            <td>{{ row.projectId }}</td>
            <td>{{ row.timeWorkedTogether }}</td>
            <!-- <td v-for="(value, index) in row" :key="index">{{ value }}</td> -->
          </tr>
        </tbody>
      </table>
    </div>
  </template>
  
  <script>
  import axios from 'axios';
  
  export default {
    data() {
      return {
        file: null,
        data: [],
        headers: []
      };
    },
    methods: {
      onFileChange(event) {
        this.file = event.target.files[0];
      },
      async uploadFile() {
        if (!this.file) {
          alert("Please select a file first");
          return;
        }
        
        let formData = new FormData();
        formData.append('file', this.file);
  
        try {
          const response = await axios.post('http://localhost:8080/importCsv', formData, {
            headers: {
              'Content-Type': 'multipart/form-data'
            }
          });
  
          this.processData(response.data);
        } catch (error) {
          console.error("There was an error uploading the file!", error);
        }
      },
      processData(data) {
        console.log(data);
        if (Array.isArray(data) && data.length > 0) {

          this.data = data;
        } else {
            alert("The Data format is not as expected or no data was present");
        //   console.error("The data format is not as expected!");
        }
      }
    }
  };
  </script>
  
  <style scoped>
  table {
    width: 100%;
    border-collapse: collapse;
  }
  th, td {
    border: 1px solid #ddd;
    padding: 8px;
  }
  th {
    background-color: #f4f4f4;
  }
  </style>
  