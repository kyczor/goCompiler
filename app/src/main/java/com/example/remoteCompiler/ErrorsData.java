package com.example.remoteCompiler;

public class ErrorsData
{
    public boolean Ok;
    public String Errors;

    ErrorsData(boolean compiled, String errorsList)
    {
        this.Ok = compiled;
        this.Errors = errorsList;
    }

    public String getErrorsList() {
        return Errors;
    }

    public boolean getCompiled() {
        return Ok;
    }
}
