$(document).ready(


function() {

    var msg = $("body").messageBox({
        autoClose : 5,
        showAutoClose : true,
        modal:true,
        css: 'css/messageBox.css',
        // called when message box is closed
        cbClose: function() {
            $("label").fadeOut(2000);
        },
        // called when message box is ready
        cbReady: function() {
        },
        // localization
        locale:{
          NO : 'No',
          YES : 'Yes',
          CANCEL : 'Cancel',
          OK : 'Okey',
          textAutoClose: 'Auto close in %d seconds'
        }
    });

    String.prototype.replaceAt=function(index, replacement) {
        return this.substr(0, index) + replacement+ this.substr(index + replacement.length);
    }

    var isDeviceError = false;
    var status;

    // set the RLY-8 device name
    $(".logo").delay(1000).fadeIn(function() {
        $.get( "rly8/name", function( response ) {
            if(response.length < 100) {
                $(".logo").text(response)
            }
            else {
                console.error("Device name is too long!")
            }
        });
    });

    // load the relay statuses
    $.get( "rly8/get", function( response ) {
      status = response;
      if(status.length < 8) {
      console.log("Cannot get relay status: " + status)
          isDeviceError = true;
      }
      var i = 0;
      $('#rly8-form input[type=checkbox]').each(function () {
        if(isDeviceError) {
          this.disabled = true;
          msg.data('messageBox').danger('Data error', 'Cannot get data from the device! After you fixed the problem please reload the page.');
        }
        else {
          var bit = status.substr(i, 1);
          if(bit == "0") {
            this.checked=false;
          }
          if(bit == "1") {
            this.checked=true;
          }
          i = i+1;
        }
      });

    });


    $('#rly8-form input[type=checkbox]').change(function() {
          var idx = parseInt(this.id.substr(5, 1)) - 1;
          if(this.checked) {
              status = status.replaceAt(idx, "1");
          }
          else {
              status = status.replaceAt(idx, "0");
          }
          $.post( "rly8/set?status="+status, function(isOk) {
              if(!isOk) {
                console.log("Cannot set relay to: "+status)
                msg.data('messageBox').danger('Data error', 'Cannot send data to the device! After you fixed the problem please reload the page.');
              }
          });
    });


}


);