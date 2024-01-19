/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleResult(resultData) {

    console.log("handleResult: populating movie table from resultData");
    let movieInfoElement = jQuery("#movie_info");
    movieInfoElement.html("<h1>" + resultData["movie_title"] + "</h1>" +
                        "<button onClick='addToCart(\"" + resultData["movie_id"] +
                        "\")'>Add to Cart</button>" +
                        "<p>Year: " + resultData["movie_year"] + "</p>" +
                        "<p>Director: " + resultData["movie_director"] + "</p>");

    console.log("handleResult: populating ratings info from resultData");
    let ratingsInfoElement = jQuery("#ratings_info");
    ratingsInfoElement.html("<p>Rating: " + resultData["rating"] +
                                " <span class=\"votes-subtitle\">" + resultData["num_votes"] + " Votes</span></p>");

    console.log("handleResult: populating genre table from resultData");
    let genreTableElement = jQuery("#genres_info");
    let genreNames = resultData["genre_name"];
    let genreNamesString = "<p> Genres: " + genreNames[0];
    for (let i = 1; i < genreNames.length; i++) {
        genreNamesString += ', ' + genreNames[i];}
    genreNamesString += "</p>";
    genreTableElement.html(genreNamesString);

    console.log("handleResult: populating star table from resultData");
    let starTableElement = jQuery("#stars_table_body");
    let starNames = resultData["stars_name"];
    let starIds = resultData["stars_id"];
    for (let i = 0; i < starNames.length;i++) {
        let rowHTML= "<tr><th>" +
            '<a href="single-star.html?id=' + starIds[i] + '">' + starNames[i] +'</a>'
            + "</th></tr>";
        starTableElement.append(rowHTML);}
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let movieId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by StarsServlet in Star.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});