/**
 * logInController - Controller that handles login and registration page.
 */

angular.module('mainPageApp').controller('logInController', ['$scope', '$http','$window', function($scope, $http, $window) {
	 	
	$scope.logInError = "";
	
	// send login information to server, if successful move to main page
	$scope.logIn = function() {
		$http({
			method : "POST",
			url : "LogAndRegServlet/Login",
			params: { 	userName: $scope.logUserName,
						password: $scope.logPassword },
			headers: {'Content-Type': 'application/x-www-form-urlencoded'}
		}).success(function(response){
			if (response.success == "true"){
				$window.location.href = '/AllThatMusicGear/questions.html#/newquestions';
			}
			else {
				$scope.logInError = response.errorMsg;
				$("#logInError").removeClass("hidden");
			}
		})						
	}
	
	// send registration information to server, if successful move to main page
	$scope.register = function(){ 
		$http({
			method : "POST",
			url : "LogAndRegServlet/Register",
			params: { 	userName: 	$scope.regUserName,
						password: 	$scope.regPassword,
						nickName: 	$scope.regNickName,
						description:$scope.regDesc,
						phtoUrl: 	$scope.regPhoto },
			headers: {'Content-Type': 'application/x-www-form-urlencoded'}
		}).success(function(response){
			if (response.success == "true"){
				$window.location.href = '/AllThatMusicGear/questions.html#/newquestions';
			}
			else {
				$scope.registerError = response.errorMsg;
				$("#regError").removeClass("hidden");
			}
		})
	}
	
	// We have two windows in log-in page, one for registration
	// and one for login, the user can click to move between them,
	// clicking will call the following functions
	$scope.showReg = function(){
		$("#logInForm").addClass('hidden');
		$("#registerForm").removeClass('hidden');
	}
	$scope.showLogin = function(){
		$("#logInForm").removeClass('hidden');
		$("#registerForm").addClass('hidden');
	}
	
	$scope.showLogin();
	
	$scope.clearLogInError = function(){
		$scope.logInError = "";
		$("#logInError").addClass("hidden");
	}
	
	$scope.clearRegisterError = function(){
		$scope.registerError = "";
		$("#regError").addClass("hidden");
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
	
 }]);
