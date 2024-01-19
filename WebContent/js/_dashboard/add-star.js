
function displayResponse(response) {
    let responseMessage = jQuery("#responseMessage");
    responseMessage.html("<p>Status: " + response.status + "</p><p>Message: " + response.message + "</p>");
    clearForm();
}

function clearForm() {
    document.getElementById("star_name").value = "";
    document.getElementById("star_birth_year").value = "";
}

function addStar() {
    console.log("adding star")
    let star_name = document.getElementById("star_name").value;
    let star_birth_year = document.getElementById("star_birth_year").value;
    // Make the HTTP POST request and register the success callback function
    jQuery.ajax({
        url: "../_dashboard/api/add-star",
        method: "POST",
        dataType: "json",
        data: {star_name: star_name, star_birth_year: star_birth_year},
        success: function(data) {displayResponse(data);},
        error: function (xhr, status, error) {
            console.error("AJAX request failed:", status, error);
        }
    });
}
