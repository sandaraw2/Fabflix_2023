function redirect(){
    window.location.href = "movielist.html";
}

function genreMovies(genre){
    console.log("browsing by genre")
    // Make the HTTP GET request and register the success callback function
    jQuery.ajax({
        dataType: "json", // Setting return data type
        data:{genre: genre},
        method: "GET", // Setting request method
        url: "api/browse", // Setting request URL
        success: () => redirect() // Setting callback function
    });
}

function handleMovieListResult(resultData, success, jqXHR) {
    console.log("handleMovieListResult: populating movie table from resultData");
    let movieTableBodyElement = jQuery("#movie_table_body");
    movieTableBodyElement.empty();

    for (let i = 0; i < resultData.length; i++) {
        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th>";
        rowHTML += '<a href=' + '"single-movie.html?id=' + resultData[i]["movie_id"] +'">';
        rowHTML += resultData[i]["movie_title"];
        rowHTML += '</a>';
        rowHTML += "</th>";
        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th><th>";

        let starNames = resultData[i]["stars_name"];
        let starIds = resultData[i]["stars_id"];
        rowHTML += '<a href= "single-star.html?id=' + starIds[0] + '">' + starNames[0] +'</a>';
        for (let j = 1; j < Math.min(3, starIds.length); j++) {
            rowHTML +=  ', <a href= "single-star.html?id=' + starIds[j]  + '">'+ starNames[j]  +'</a>';
        }
        rowHTML += "</th><th>";

        let genreNames = resultData[i]["movie_genres"];
        rowHTML += '<a onclick = genreMovies("' + genreNames[0] + '")>' + genreNames[0]  + '</a>';
        for (let j = 1; j < Math.min(3, genreNames.length); j++) {
            rowHTML +=  ',' + '<a onclick = genreMovies("' + genreNames[j] + '")>' + genreNames[j]  + '</a>';
        }
        rowHTML += "</th><th>" + resultData[i]["movie_rating"] + "</th>";
        rowHTML += "<th><button onClick='addToCart(\"" + resultData[i]["movie_id"] + "\")'>Add</button></th>";
        rowHTML += "</tr>";

        movieTableBodyElement.append(rowHTML);
    }

    //<button onClick=addToCart("1757328")> Add </button>

    var pageNumber = jqXHR.getResponseHeader("pageNumber");
    var lastPage = jqXHR.getResponseHeader("lastPage");

    let paginationElement = jQuery("#pagination");
    paginationElement.empty();
    let buttonHTML = "";
    if (pageNumber !== "1"){
        buttonHTML += "<button onClick=\"previousPage()\">Previous</button>";}
    buttonHTML += "<span>"+ pageNumber+"</span>";
    buttonHTML += "<button onClick=\"nextPage()\">Next</button>";

    paginationElement.append(buttonHTML);
}


function updateSorting(){
    var selectedValue = document.getElementById("sorting").value;
    console.log("Sorting By " + selectedValue);
    jQuery.ajax({
        dataType: "json", // Setting return data type
        data: {sortBy : selectedValue},
        method: "GET", // Setting request method
        url: "api/sortBy", // Setting request url, which is mapped by StarsServlet in Star.java
        success: () => redirect() // Setting callback function to handle data returned successfully by the StarsServlet
    });
}


jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/movies", // Setting request url, which is mapped by StarsServlet in Star.java
    success: (resultData, success, jqXHR) => handleMovieListResult(resultData, success, jqXHR) // Setting callback function to handle data returned successfully by the StarsServlet
});