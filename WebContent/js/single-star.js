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


function handleResult(resultData) {

    console.log("handleResult: populating star info from resultData");
    let starInfoElement = jQuery("#star_info");
    let starInfoString = "<h1>" + resultData[0]["star_name"] + "</h1>" +
                                "<p>Date Of Birth: ";
    if (resultData[0]["star_dob"]){
        starInfoString += resultData[0]["star_dob"] + "</p>";}
    else{
        starInfoString += "N/A</p>";}

    starInfoElement.html(starInfoString);

    console.log("handleResult: populating movie table from resultData");
    let movieTableBodyElement = jQuery("#movie_table_body");
    for (let i = 0; i < resultData.length; i++) {
        let rowHTML = '<tr><th><a href="single-movie.html?id=' + resultData[i]["movie_id"] + '">'
            + resultData[i]["movie_title"] + '</a></th></tr>';

        movieTableBodyElement.append(rowHTML);
    }
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let starId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-star?id=" + starId, // Setting request url, which is mapped by StarsServlet in Star.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});