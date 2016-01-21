 
 var logInApp = angular.module('logInApp',[]);
 logInApp.controller('logInAppController', ['$scope', '$http', function($scope, $http) {
		$scope.logedInUserNickName = "";
		$scope.http = $http;
		
		$scope.logIn = function() {
			/* first check userName */
			for (x in $scope.allUsers) {
				if ($scope.allUsers[x].userName == $scope.logUserName){
					if ($scope.allUsers[x].password == $scope.logPassword){
						/* good LogIn */
						$scope.logedInUserNickName = "Current Logged In User NickName: " + $scope.allUsers[x].nickName;
						$scope.logInError = "Logged in - Take care to move to main question view";
						return;
					}
					else{
						$scope.logInError = "Password Incorrect";
						return;
					}	
				}
			}
			$scope.logInError = "User Name Doesn't Exist";
		}
		
		$scope.register = function(){ 			
			var parameters = {
					params: {
						userName: $scope.regUserName,
						password: $scope.regPassword,
						nickName: $scope.regNickName,
						description: $scope.regDesc,
						phtoUrl: $scope.regPhoto						
					}
			};
			
			$scope.http.get("http://localhost:8080/AllThatMusicGear/LogAndRegServlet/Register", parameters)
			.success(function(response) {			
				$scope.logedInUserNickName = "Current Logged In User NickName: " + response[0].nickName;
				$scope.resetRegFields();
				$scope.updateAllUserTable();
			})
		}
		
		$scope.updateAllUserTable = function()
		{
			$scope.http.get("http://localhost:8080/AllThatMusicGear/LogAndRegServlet/Users")
			.success(function(response) {			
				$scope.allUsers = response;
				$scope.counter = 0;
				for (x in $scope.allUsers) {
					$scope.counter = $scope.counter + 1;
				}
			});
		}
		
		$scope.clearLogInError = function(){
			$scope.logInError = "";
		}
		
		$scope.clearRegisterError = function(){
			$scope.registerError = "";
		}
		
		$scope.resetLogFields = function() {
			$scope.logUserName = "";
			$scope.logPassword = "";
			$scope.clearLogInError();
		}

		$scope.resetRegFields = function(){
			
			$scope.regUserName = "";
			$scope.regPassword = "";
			$scope.regNickName = "";
			$scope.regDesc = "";
			$scope.regPhoto = "";
		}
		
	     
	    $scope.validateUserName = function() {
	    	$scope.clearRegisterError();
	    	for (x in $scope.allUsers) {
				if ($scope.allUsers[x].userName == $scope.regUserName){
					$scope.registerError = "User Name Already Exists";
				}
	    	}
	    }
	    
	    $scope.validateNickName = function() {
	    	$scope.clearRegisterError();
	    	for (x in $scope.allUsers) {
				if ($scope.allUsers[x].nickName == $scope.regNickName){
					$scope.registerError = "NickName Already Exists";
				}
	    	}
	    }
	      
		
		$scope.resetLogFields();
		$scope.clearLogInError();
		$scope.clearRegisterError();
		$scope.updateAllUserTable();
		
	}]);