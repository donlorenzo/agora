/*
 * Copyright 2015 by Lorenz Quack
 *
 * This file is part of agora.
 *
 *     agora is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 2 of the License, or
 *     (at your option) any later version.
 *
 *     agora is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with agora.  If not, see <http://www.gnu.org/licenses/>.
 */

function POST(uri, data, successCallback, errorCallback) {
    alert(uri);
    $.ajax({"url": "http://localhost:8080/api/" + uri, "method": "POST", "data": data}).done(function(data, status, xhr) {
                                                                              if (successCallback) {successCallback(data, status, xhr);}
                                                                            })
                                                                            .fail(function(xhr, status, exc) {
                                                                              if (errorCallback) {errorCallback(xhr, status. exc);}                                                                            });
}

function GET(uri) {
    alert(uri);
    $.ajax({url: "http://localhost:8080/api/" + uri, method: "GET", data: data});
}
