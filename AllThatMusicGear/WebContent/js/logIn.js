var logIn = angular.module('logIn',[]);
logIn.controller('logInController', ['$scope', '$http','$window', function($scope, $http, $window) {
	 	
	$scope.logInError = "";
	
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
	
	$scope.showReg = function(){
		$("#logInForm").addClass('hidden');
		$("#registerForm").removeClass('hidden');
	}
	$scope.showLogin = function(){
		$("#logInForm").removeClass('hidden');
		$("#registerForm").addClass('hidden');
	}
	
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