app.factory('data', ['$http', function($http) {
    var urlBase = "/simulate/rest/customer/";
    var data = {};
    
    data.getClients = function(){return $http.get(urlBase + 'list');};
    data.UploadCust = function(file,cert,logo,background,cid){
         var formData=new FormData();
         formData.append("file", file); //key
         formData.append("cert", cert);
         formData.append("logo", logo);
         formData.append("background", background);
         formData.append("cid", cid);
         console.log( " service cid " +cid+ " keyfile " +file + " cert " +cert +" logo "+logo +" background "+background);
	     return $http({
	        		method: 'POST',
	        		url: '/simulate/rest/customer/upload', // The URL to Post.
	        		headers: {'Content-Type': undefined}, // Set the Content-Type to undefined always.
	        		data: formData,
	        		transformRequest: function(data, headersGetterFunction) {
	        			console.log("success upload");
	            		return data;
	        		}
	     })
    };
    data.updateClient = function(datas){return $http.post(urlBase + 'save', datas);};
    data.newClientData = function(datas){return $http.post(urlBase + 'save', datas);};
    data.deleteClientData = function(datas){return $http.post(urlBase + 'delete', datas);};
    
    //Accounts
    
    data.myProfile = function(){return $http.get("/simulate/rest/user/profile");};
    data.updateMyProfile = function(datas){return $http.post('/simulate/rest/user/save', datas);};
    data.changePassword = function(datas){return $http.post('/simulate/rest/user/profile/chpwd', datas);};
    
    //Guest Pass
    data.guestPass = function(){return $http.get("/simulate/rest/gustpass/list");};
    data.network = function(){return $http.get("/simulate/rest/gustpass/networks");};
    data.newguestPass = function(datas){return $http.post("/simulate/rest/gustpass/save", datas);};
    data.updateguestPass = function(datas){return $http.post("/simulate/rest/gustpass/save", datas);};
    data.deleteguestPass = function(datas){return $http.post("/simulate/rest/gustpass/delete/", datas);};
    data.updateGuestStatus = function(datas){return $http.post("/simulate/rest/site/updategueststatus", datas);};
 
    //Get inactive customer
    data.getcustinactiveData = function(){return $http.get("/simulate/rest/customer/inactive");};
    data.updateCustData = function(datas){return $http.post("/simulate/rest/customer/active", datas);}
    data.clientList = function(){return $http.get("/simulate/rest/customer/customerList");};

    //Get Licence
     data.getLicenceData = function(){return $http.get(urlBase + 'licence');};
     data.deactivateLicense = function(datas){ return $http.post(urlBase + 'deactivate',datas);};
     
     //email support details
     data.getSupportMailData = function(){return $http.get("/simulate/rest/customer/supportDetails");};
     
    //Get Notifications
    data.getNotificationEmail = function(){return $http.get(urlBase + 'list');};
    data.getNotificationSms = function(){return $http.get(urlBase + 'list');};
    
    //Support
    data.getSupportData = function(){return $http.get("/simulate/rest/customer/supportlist");};
    data.updateSupportData = function(datas){return $http.post("/simulate/rest/customer/support", datas);};

    
    
    
    //Roles
    data.getRoleData = function(){return $http.get("/simulate/rest/role/list");};
    data.createNewRoles = function(datas){return $http.post("/simulate/rest/role/save", datas);};
    data.updateNewRoles = function(datas){return $http.post("/simulate/rest/role/save", datas);};
    
    //Users
    data.getUsersData = function(){return $http.get("/simulate/rest/user/list");};
    data.updateUsersData = function(datas){return $http.post("/simulate/rest/user/save",datas);};
    data.deleteUserData = function(datas){return $http.post("/simulate/rest/user/delete",datas);};
    data.pwdUser = function(datas){return $http.post("/simulate/rest/user/chpwd",datas);};
    
    //roles
    data.getRolesData = function(){return $http.get('/simulate/rest/privilege/fetch');};
    //timezone
    data.timezone = function(){return $http.get("/simulate/rest/customer/timeZone");};
    data.cloudlog = function(datas){return $http.post("/simulate/rest/customer/cloudlog", datas);};
    
    //vpn
    data.openVpn = function(datas){return $http.post("/simulate/rest/customer/vpn", datas);};

    data.checkEmailDuplicate = function(datas){return $http.get("/simulate/rest/user/checkDuplicateUID?email="+datas);};
    	
    return data;
}]);