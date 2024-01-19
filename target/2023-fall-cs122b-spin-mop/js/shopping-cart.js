function increaseCount(movieID){
    console.log("Adding Movie to Cart");

    jQuery.ajax({
        method: "POST",
        data:{decrease: false,
            deleteID: false},
        url: "api/addToCart?id=" + movieID,
        error: function(xhr, status, error) {
            console.error('Error:', error);}
    });

    console.log("Reloading Page");
    jQuery.ajax({
        dataType: "json",  // Setting return data type
        method: "GET",// Setting request method
        url: "api/shopping-cart",
        success: (resultData, success, jqXHR) => handleResult(resultData, success, jqXHR)
    });
}

function decreaseCount(movieID){
    console.log("Adding Movie to Cart");

    jQuery.ajax({
        method: "POST",
        data:{decrease: true,
            deleteID: false},
        url: "api/addToCart?id=" + movieID,
        error: function(xhr, status, error) {
            console.error('Error:', error);}
    });

    console.log("Reloading Page");
    jQuery.ajax({
        dataType: "json",  // Setting return data type
        method: "GET",// Setting request method
        url: "api/shopping-cart",
        success: (resultData, success, jqXHR) => handleResult(resultData, success, jqXHR)
    });
}

function deleteMovie(movieID){
    console.log("Deleting Movie");

    jQuery.ajax({
        method: "POST",
        data:{decrease: false,
            deleteID: true},
        url: "api/addToCart?id=" + movieID,
        error: function(xhr, status, error) {
            console.error('Error:', error);}
    });

    console.log("Reloading Page");
    jQuery.ajax({
        dataType: "json",  // Setting return data type
        method: "GET",// Setting request method
        url: "api/shopping-cart",
        success: (resultData, success, jqXHR) => handleResult(resultData, success, jqXHR)
    });
}

function handleResult(resultData, success, jqXHR) {
    console.log("handleResult: populating cart with resultData");
    let cartTableBodyElement = jQuery("#cart_table_body");
    cartTableBodyElement.empty();

    for (let i = 0; i < resultData.length; i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th>" + resultData[i]["movie_title"] + "</th>";
        rowHTML += "<th>" + "<button onClick='decreaseCount(\"" + resultData[i]["movie_id"] + "\")'> - </button>";
        rowHTML += resultData[i]["quantity"];
        rowHTML += "<button onClick='increaseCount(\"" + resultData[i]["movie_id"] + "\")'> + </button>" + "</th>";
        rowHTML += "<th>" + resultData[i]["price"] + "</th>";
        rowHTML += "<th>" + "<button onClick='deleteMovie(\"" + resultData[i]["movie_id"] + "\")'>Delete</button>" + "</th>";
        rowHTML += "</tr>";
        cartTableBodyElement.append(rowHTML);
    }


    var totalPrice = jqXHR.getResponseHeader("totalPrice");
    let cartTotalElement = jQuery("#cart_total");
    cartTotalElement.empty();
    cartTotalElement.append("<p> Total: $" + totalPrice + " </p>");
    localStorage.setItem("totalPrice", totalPrice);
}

jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/shopping-cart",
    success: (resultData, success, jqXHR) => handleResult(resultData, success, jqXHR)
});

