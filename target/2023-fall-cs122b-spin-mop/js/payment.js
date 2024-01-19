let payment_form = jQuery("#payment_form");

function handlePaymentResult(resultDataJson, status, jqXHR) {
    console.log(resultDataJson);

    console.log("handle payment response");
    console.log(resultDataJson);
    console.log(jqXHR.getResponseHeader("startID"));

    console.log(resultDataJson["status"]);
    if (resultDataJson["status"] === "success") {
        const startID = jqXHR.getResponseHeader("startID");
        const itemCount = jqXHR.getResponseHeader("itemCount");

        localStorage.setItem("startID", startID);
        localStorage.setItem("itemCount", itemCount);

        window.location.replace("confirmation.html");
    } else {
        console.log("show error message");
        console.log(resultDataJson["message"]);

        payment_form[0].reset()
        jQuery("#payment_error_message").text(resultDataJson["message"]).css("color", "red");
    }
}

function submitPaymentForm(formSubmitEvent) {
    console.log("submit payment form");
    formSubmitEvent.preventDefault();
    jQuery.ajax("api/payment",{
            method: "POST",
            data: payment_form.serialize(),
            success: handlePaymentResult
        }
    );
}

payment_form.submit(submitPaymentForm);