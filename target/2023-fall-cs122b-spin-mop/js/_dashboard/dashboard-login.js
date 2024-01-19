let dashboard_login_form = jQuery("#dashboard_login_form");

function handleLoginResult(resultDataJson) {
  console.log(resultDataJson);
  /*let resultDataJson = JSON.parse(resultDataString);*/

  console.log("handle login response");
  console.log(resultDataJson);
  console.log(resultDataJson["status"]);

  if (resultDataJson["status"] === "success") {
      window.location.replace("dashboard-index.html");
  } else {
    console.log("show error message");
    console.log(resultDataJson["message"]);
    jQuery("#login_error_message").text(resultDataJson["message"]).css("color", "red");
  }
}

function submitLoginForm(formSubmitEvent) {
  console.log("submit login form");
  formSubmitEvent.preventDefault();
  jQuery.ajax("api/dashboard-login",{
        method: "POST",
        data: dashboard_login_form.serialize(),
        success: handleLoginResult
      }
  );
}

dashboard_login_form.submit(submitLoginForm);