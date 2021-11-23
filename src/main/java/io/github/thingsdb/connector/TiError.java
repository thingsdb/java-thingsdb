package io.github.thingsdb.connector;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.core.MessagePackException;

import io.github.thingsdb.connector.exceptions.AssertionError;
import io.github.thingsdb.connector.exceptions.AuthError;
import io.github.thingsdb.connector.exceptions.BadRequestError;
import io.github.thingsdb.connector.exceptions.CancelledError;
import io.github.thingsdb.connector.exceptions.ForbiddenError;
import io.github.thingsdb.connector.exceptions.InternalError;
import io.github.thingsdb.connector.exceptions.LookupError;
import io.github.thingsdb.connector.exceptions.MaxQuotaError;
import io.github.thingsdb.connector.exceptions.MemoryError;
import io.github.thingsdb.connector.exceptions.NodeError;
import io.github.thingsdb.connector.exceptions.NumArgumentsError;
import io.github.thingsdb.connector.exceptions.OperationError;
import io.github.thingsdb.connector.exceptions.OverflowError;
import io.github.thingsdb.connector.exceptions.RequestCancelError;
import io.github.thingsdb.connector.exceptions.RequestTimeoutError;
import io.github.thingsdb.connector.exceptions.ResultTooLargeError;
import io.github.thingsdb.connector.exceptions.SyntaxError;
import io.github.thingsdb.connector.exceptions.TiException;
import io.github.thingsdb.connector.exceptions.TypeError;
import io.github.thingsdb.connector.exceptions.UnpackError;
import io.github.thingsdb.connector.exceptions.ValueError;
import io.github.thingsdb.connector.exceptions.WriteUVError;
import io.github.thingsdb.connector.exceptions.ZeroDivError;

class TiError {
    static TiException fromData(ByteBuffer data) {
        MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(data);
        try {
            TiException exp;

            int numItems = unpacker.unpackMapHeader();
            if (numItems != 2) {
                throw new MessagePackException("Expecting a map with 2 items");
            }
            unpacker.unpackString();  // error_code
            int code = unpacker.unpackInt();

            unpacker.unpackString();  // error_msg
            String msg = unpacker.unpackString();

            switch (code) {
                // Normal ThignsDB Error code
                case -64: exp = new CancelledError(msg); break;
                case -63: exp = new OperationError(msg); break;
                case -62: exp = new NumArgumentsError(msg); break;
                case -61: exp = new TypeError(msg); break;
                case -60: exp = new ValueError(msg); break;
                case -59: exp = new OverflowError(msg); break;
                case -58: exp = new ZeroDivError(msg); break;
                case -57: exp = new MaxQuotaError(msg); break;
                case -56: exp = new AuthError(msg); break;
                case -55: exp = new ForbiddenError(msg); break;
                case -54: exp = new LookupError(msg); break;
                case -53: exp = new BadRequestError(msg); break;
                case -52: exp = new SyntaxError(msg); break;
                case -51: exp = new NodeError(msg); break;
                case -50: exp = new AssertionError(msg); break;

                // Build-in ThingsDB error codes
                case -6: exp = new ResultTooLargeError(msg); break;
                case -5: exp = new RequestTimeoutError(msg); break;
                case -4: exp = new RequestCancelError(msg); break;
                case -3: exp = new WriteUVError(msg); break;
                case -2: exp = new MemoryError(msg); break;
                case -1: exp = new InternalError(msg); break;

                default: exp = new UnpackError(String.format("Unknown error type %d", code));
            }

            unpacker.close();
            return exp;
        } catch (MessagePackException | IOException e) {
            return new UnpackError(e.getMessage());
        }
    }
}
