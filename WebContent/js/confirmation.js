function handleConfirmationResult(resultData, success, jqXHR){
    console.log("handling Confirmation Result");
    let confirmationTableBodyElement = jQuery("#confirmation_table_body");
    confirmationTableBodyElement.empty();

    for (let i = 0; i < resultData.length; i++) {
        let rowHTML = "<tr>";
        rowHTML += "<th>" + resultData[i]["sale_id"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_title"] + "</th>";
        rowHTML += "<th>" + resultData[i]["quantity"] + "</th>";
        rowHTML += "<th>" + resultData[i]["price"] + "</th>";
        rowHTML += "</tr>";

        confirmationTableBodyElement.append(rowHTML);
    }

    var totalPrice = jqXHR.getResponseHeader("totalPrice");
    let cartTotalElement = jQuery("#cart_total");
    cartTotalElement.empty();
    cartTotalElement.append("<p> Total: $" + totalPrice + " </p>");
    localStorage.setItem("totalPrice", totalPrice);
}

console.log(localStorage.getItem("startID"));
console.log(localStorage.getItem("itemCount"));

jQuery.ajax({
    dataType: "json",  // Setting return data type
    data: {startID:  localStorage.getItem("startID"),
        itemCount: localStorage.getItem("itemCount")},
    method: "GET",// Setting request method
    url: "api/confirmation",
    success: function(resultData, success, jqXHR){
        console.log("Success loading confirmation");
        handleConfirmationResult(resultData, success, jqXHR);
    },
    error: function(jqXHR, textStatus, errorThrown) {
        console.log("AJAX request failed");
        console.log("Status: " + textStatus);
        console.log("Error: " + errorThrown);}
});