// metadata.js
document.addEventListener('DOMContentLoaded', function() {
    fetchMetadata();
});

function fetchMetadata() {
    jQuery.ajax({
        url: "api/get-meta-data",
        method: "GET",
        dataType: "json",
        success: function(data) {displayMetadata(data);},
        error: function (xhr, status, error) {
            console.error("AJAX request failed:", status, error);
        }
    });
}

function displayMetadata(metadata) {
    const metadataDiv = document.getElementById('metadata');

    if (metadata["error"]) {
        metadataDiv.textContent = 'Error fetching metadata: ' + metadata["error"];
        return;
    }

    const tables = metadata["tables"];
    tables.forEach(table => {
        const tableDiv = document.createElement('div');
        tableDiv.innerHTML = `<h2>${table["tableName"]}</h2>`;

        const columns = table["columns"];
        const columnsList = document.createElement('ul');
        columns.forEach(column => {
            const columnItem = document.createElement('li');
            columnItem.textContent = `${column["columnName"]} (${column["columnType"]})`;
            columnsList.appendChild(columnItem);
        });

        tableDiv.appendChild(columnsList);
        metadataDiv.appendChild(tableDiv);
    });
}
