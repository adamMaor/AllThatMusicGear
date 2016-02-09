var logIn = angular.module('logIn',[]);
logIn.controller('logInController', ['$scope', '$http','$window', function($scope, $http, $window) {
	 		 	
	$scope.logIn = function() {
		if ($scope.logUserName == "" || $scope.logPassword == "") {
			$scope.logInError = "Please provide a username and password";
		}
		else{
			$http({
				method : "POST",
				url : "http://localhost:8080/AllThatMusicGear/LogAndRegServlet/Login",
				params: { 	userName: $scope.logUserName,
							password: $scope.logPassword },
				headers: {'Content-Type': 'application/x-www-form-urlencoded'}
			}).success(function(response){
				$scope.logInError = response;
				if ($scope.logInError == ""){
					$window.location.href = '/AllThatMusicGear/newquestions.html';
				}
			})				
		}			
	}
	
	$scope.register = function(){ 
		
		if ($scope.regUserName == "" || $scope.regPassword =="" || $scope.regNickName== ""){
			$scope.registerError = "Must Fill UserName + Password + NickName";
			return;
		}
		else {
			$http({
				method : "POST",
				url : "http://localhost:8080/AllThatMusicGear/LogAndRegServlet/Register",
				params: { 	userName: $scope.regUserName,
							password: $scope.regPassword,
							nickName: $scope.regNickName,
							description: $scope.regDesc,
							phtoUrl: $scope.regPhoto },
				headers: {'Content-Type': 'application/x-www-form-urlencoded'}
			}).success(function(response){
				$scope.registerError = response;
				if ($scope.registerError == ""){
					$window.location.href = '/AllThatMusicGear/newquestions.html';
				}	
			})
			
		}
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
		$scope.clearRegisterError();
	}
	
	$scope.resetLogFields();
	$scope.resetRegFields();

 }]);