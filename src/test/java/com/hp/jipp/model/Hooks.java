package com.hp.jipp.model;

import com.hp.jipp.util.Hook;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

class Hooks implements TestRule {
    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                base.evaluate();
                Hook.reset();
            }
        };
    }
}
