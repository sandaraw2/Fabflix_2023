function updatePagination(){
    console.log("Updating Pages");

    jQuery.ajax({
        dataType: "json",
        data:{newNumMovies: document.getElementById("numberOfMovies").value},
        method: "GET",
        url: "api/movies",
        success: (resultData, success, jqXHR) => handleMovieListResult(resultData, success, jqXHR)
    });
}

function previousPage(){
    jQuery.ajax({
        dataType: "json",
        data:{changePage: -1},
        method: "GET",
        url: "api/movies",
        success: (resultData, success, jqXHR) => handleMovieListResult(resultData, success, jqXHR)
    });
}

function nextPage(){
    jQuery.ajax({
        dataType: "json",
        data:{changePage: 1},
        method: "GET",
        url: "api/movies",
        success: (resultData, success, jqXHR) => handleMovieListResult(resultData, success, jqXHR)
    });

}