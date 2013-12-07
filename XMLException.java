//
// You received this file as part of RRLib
// Robotics Research Library
//
// Copyright (C) Finroc GbR (finroc.org)
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
//
//----------------------------------------------------------------------
package org.rrlib.xml;

/**
 * Exceptions thrown in rrlib_xml are of this type.
 * This exception class is a java.lang.Exception and used when invalid
 * XML input is encountered that can not be handled automatically.
 * Thus, catching exceptions of this type distinguishes from other runtime
 * errors.
 */
public class XMLException extends Exception {

    /** UID */
    private static final long serialVersionUID = -3624705191730900543L;

    /**
     * This ctor forwards instantiation of an exception object to
     * java.lang.Exception with the given message as error description.
     *
     * @param message   A message that describes the error
     */
    public XMLException(String message) {
        super(message);
    }

    /**
     * @param e Exception cause
     */
    public XMLException(Exception e) {
        super(e);
    }
}
