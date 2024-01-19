
function displayResponse(response) {
    let responseMessage = jQuery("#responseMessage");
    responseMessage.html("<p>Status: " + response.status + "</p><p>Message: " + response.message + "</p>");
    clearForm();
}

function clearForm() {
    document.getElementById("movieTitle").value = "";
    document.getElementById("movieYear").value = "";
    document.getElementById("movieDirector").value = "";
    document.getElementById("starName").value = "";
    document.getElementById("starBirthYear").value = "";
    document.getElementById("genreName").value = "";
}


function addMovie(){
    console.log("adding movie")
    let movie_title = document.getElementById("movieTitle").value;
    let movie_year = document.getElementById("movieYear").value;
    let movie_director = document.getElementById("movieDirector").value;
    let star_name = document.getElementById("starName").value;
    let star_birth_year = document.getElementById("starBirthYear").value;
    let genre_name  = document.getElementById("genreName").value;

    // Make the HTTP POST request and register the success callback function
    jQuery.ajax({
        url: "../_dashboard/api/add-movie",
        method: "POST",
        dataType: "json",
        data: {movie_title: movie_title,
                movie_year: movie_year,
                movie_director: movie_director,
                star_name: star_name,
                star_birth_year: star_birth_year,
                genre_name: genre_name},
        success: function(data) {displayResponse(data);},
        error: function (xhr, status, error) {
            console.error("AJAX request failed:", status, error);
        }
    });

}