let advance_search_form = jQuery("#advance_search_form");
// Event listener for input changes

function displayAdvancedSearchResults(resultData) {
    // Redirect to the movie link
    console.log("switching page");
    window.location.href = "movielist.html";
}

// Add an event listener for the "click" event
function submitAdvancedSearch(formSubmitEvent) {
    // Makes the HTTP GET request and registers on success callback function handleStarResult
    console.log("submit advanced search form");
    formSubmitEvent.preventDefault();
    jQuery.ajax("api/advancedSearch", {
        dataType: "json", // Setting return data type
        data: advance_search_form.serialize(),
        method: "GET", // Setting request method
        success: displayAdvancedSearchResults,
        error: function(xhr, status, error) {
            console.log("Error");}
    });
}

// when form is submitted submitAdvancedSearch form will be called
advance_search_form.submit(submitAdvancedSearch);
